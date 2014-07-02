
-- Standard variable summary information modelled as per http://gmod.org/wiki/Chado_Tables#Table:_cvterm

drop view if exists standard_variable_summary;

	create view standard_variable_summary as

		select  cvt.cvterm_id as 'id', 
			cvt.name as 'name', 			
			cvt.definition as 'definition',
						
			group_concat(if(cvtr.type_id=1200, cvtr.object_id, null)) as 'property_id',
			group_concat(if(cvtr.type_id=1200, prop.name, null)) as 'property_name',
			group_concat(if(cvtr.type_id=1200, prop.definition, null)) as 'property_def',
			
			group_concat(if(cvtr.type_id=1210, cvtr.object_id, null)) as 'method_id',
			group_concat(if(cvtr.type_id=1210, method.name, null)) as 'method_name',
			group_concat(if(cvtr.type_id=1210, method.definition, null)) as 'method_def',

			group_concat(if(cvtr.type_id=1220, cvtr.object_id, null)) as 'scale_id',
			group_concat(if(cvtr.type_id=1220, scale.name, null)) as 'scale_name',
			group_concat(if(cvtr.type_id=1220, scale.definition, null)) as 'scale_def',

			group_concat(if(cvtr.type_id=1225, cvtr.object_id, null)) as 'is_a_id',			
			group_concat(if(cvtr.type_id=1225, class.name, null)) as 'is_a_name',
			group_concat(if(cvtr.type_id=1225, class.definition, null)) as 'is_a_def',

			group_concat(if(cvtr.type_id=1044, cvtr.object_id, null)) as 'stored_in_id',			
			group_concat(if(cvtr.type_id=1044, storedin.name, null)) as 'stored_in_name',
			group_concat(if(cvtr.type_id=1044, storedin.definition, null)) as 'stored_in_def',
			
			group_concat(if(cvtr.type_id=1105, cvtr.object_id, null)) as 'data_type_id',			
			group_concat(if(cvtr.type_id=1105, datatype.name, null)) as 'data_type_name',
			group_concat(if(cvtr.type_id=1105, datatype.definition, null)) as 'data_type_def',
						
			case 
				when cvtr.object_id in (1010,1011,1012) then "STUDY"
				when cvtr.object_id in (1015,1016,1017) then "DATASET"
				when cvtr.object_id in (1020,1021,1022,1023,1024,1025) then "TRIAL_ENVIRONMENT"
				when cvtr.object_id = 1030 then "TRIAL_DESIGN"
				when cvtr.object_id in (1040,1041,1042,1046,1047) then "GERMPLASM"
				when cvtr.object_id in (1043, 1048) then "VARIATE"				
			end as "phenotypic_type",
			
			case
				when cvtr.object_id in (1120,1125,1128,1130) then 'C'
				when cvtr.object_id in (1110,1117,1118) then 'N'
				else null
		    end as data_type_abbrev
	
	from cvterm cvt
		join   cvterm_relationship cvtr on cvtr.subject_id = cvt.cvterm_id
		join   cvterm prop on prop.cvterm_id = cvtr.object_id
		join   cvterm method on method.cvterm_id = cvtr.object_id
		join   cvterm scale on scale.cvterm_id = cvtr.object_id
		join   cvterm class on class.cvterm_id = cvtr.object_id
		join   cvterm storedin on storedin.cvterm_id = cvtr.object_id
		join   cvterm datatype on datatype.cvterm_id = cvtr.object_id		
	
	where cvt.cv_id = 1040
		group by cvt.cvterm_id, cvt.name
		order by id;
		
-- view for germplasm search
		
drop view if exists germplasm_summary;

	create view germplasm_summary as
	
		select gp.gid, group_concat(n.nval) as names, ld.listid
		
		from germplsm gp 
			
			inner join names n on n.gid = gp.gid
			left outer join listdata ld on ld.gid = n.gid
		
		where gp.gid != gp.grplce and n.nstat != 9
		group by gid
	;
	

drop view if exists germplasm_trial_details;
create view `germplasm_trial_details` AS
    select 
        `pr`.`object_project_id` AS `study_id`,
        `ep`.`project_id` AS `project_id`,
        `e`.`type_id` AS `type_id`,
        `e`.`nd_geolocation_id` AS `envt_id`,
        `e`.`type_id` AS `observation_type`,
        `e`.`nd_experiment_id` AS `experiment_id`,
        `p`.`phenotype_id` AS `phenotype_id`,
        `td`.`trait_name` AS `trait_name`,
        `svd`.`cvterm_id` AS `stdvar_id`,
        `svd`.`stdvar_name` AS `stdvar_name`,
        `p`.`value` AS `observed_value`,
        `s`.`stock_id` AS `stock_id`,
        `s`.`name` AS `entry_designation`,
        `g`.`gid` AS `gid`
    from
        (((((((((`stock` `s`
        join `nd_experiment_stock` `es` ON ((`es`.`stock_id` = `s`.`stock_id`)))
        join `nd_experiment` `e` ON ((`e`.`nd_experiment_id` = `es`.`nd_experiment_id`)))
        join `nd_experiment_project` `ep` ON ((`ep`.`nd_experiment_id` = `e`.`nd_experiment_id`)))
        join `nd_experiment_phenotype` `epx` ON ((`epx`.`nd_experiment_id` = `e`.`nd_experiment_id`)))
        join `phenotype` `p` ON ((`p`.`phenotype_id` = `epx`.`phenotype_id`)))
        join `standard_variable_details` `svd` ON ((`svd`.`cvterm_id` = `p`.`observable_id`)))
        join `trait_details` `td` ON ((`td`.`trait_id` = `svd`.`property_id`)))
        join `project_relationship` `pr` ON ((`pr`.`subject_project_id` = `ep`.`project_id`)))
        join `germplsm` `g` ON ((`s`.`dbxref_id` = `g`.`gid`)))
    where
        ((`e`.`type_id` = 1170)
            or ((`e`.`type_id` = 1155)
            and (1 = (select 
                count(0)
            from
                `project_relationship`
            where
                ((`project_relationship`.`object_project_id` = `pr`.`object_project_id`)
                    and (`project_relationship`.`type_id` = 1150))))))
    order by `ep`.`project_id` , `e`.`nd_geolocation_id` , `e`.`type_id` , `td`.`trait_name` , `s`.`name`;