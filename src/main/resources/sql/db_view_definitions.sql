
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